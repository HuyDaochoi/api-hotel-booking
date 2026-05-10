package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.BadRequestException;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.BookingRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.dto.response.PricingEstimateResponse;
import com.yo.apihotelbooking.schemas.domain.Booking;
import com.yo.apihotelbooking.schemas.domain.BookingStatusHistory;
import com.yo.apihotelbooking.schemas.domain.Room;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import com.yo.apihotelbooking.repository.BookingRepository;
import com.yo.apihotelbooking.repository.BookingStatusHistoryRepository;
import com.yo.apihotelbooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;
import com.yo.apihotelbooking.repository.UserRepository;
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final PricingService pricingService;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final BookingStatusHistoryRepository historyRepository;

@Transactional(rollbackFor = Exception.class)
public BookingResponse createBooking(BookingRequest request, Long userId) throws Exception {
    Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy phòng"));
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
    List<Booking> conflicts = bookingRepository.findConflictingBookings(
            room.getId(), 
            request.getCheckInDate(), 
            request.getCheckOutDate()
    );

    if (!conflicts.isEmpty()) {
        throw new BadRequestException("Phòng đã có người đặt hoặc đang chờ xác nhận trong khoảng thời gian này.");
    }

    PricingEstimateResponse pricing = pricingService.estimatePrice(
            room.getId(), request.getCheckInDate(), request.getCheckOutDate());

    Booking booking = new Booking();
    booking.setRoom(room);
    booking.setUser(user);
    booking.setCheckInDate(request.getCheckInDate());
    booking.setCheckOutDate(request.getCheckOutDate());
    booking.setTotalAmount(pricing.getTotalAmount());
    booking.setStatus(BookingStatus.PENDING);
    booking.setSpecialRequests(request.getSpecialRequests());
    booking.setNumGuests(request.getNumGuests() != null ? request.getNumGuests() : 1);
    Booking savedBooking = bookingRepository.save(booking);

    return modelMapper.map(savedBooking, BookingResponse.class);
}
    @Transactional
public void updateBookingStatus(Long bookingId, BookingStatus newStatus, User performer, String note) throws NotFoundException {
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt phòng"));

    BookingStatus oldStatus = booking.getStatus();
    
    booking.setStatus(newStatus);
    bookingRepository.save(booking);
    
    BookingStatusHistory history = new BookingStatusHistory();
    history.setBooking(booking);
    history.setOldStatus(oldStatus);
    history.setNewStatus(newStatus);
    history.setChangedBy(performer);
    history.setNote(note);
    history.setChangedAt(LocalDateTime.now());
    historyRepository.save(history);
}
}