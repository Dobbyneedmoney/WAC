package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.entity.IntrDisabled;
import com.example.demo.model.entity.Payment;
import com.example.demo.model.entity.Reservation;
import com.example.demo.model.entity.Users;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.TestUserRepository;
import com.example.demo.service.PaymentService;
import com.example.demo.service.PointService;
import com.example.demo.service.ReservationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation") 
public class ReservationController {

	private final ReservationService service;
	private final PointService pointservice;
	private final PaymentService paymentService;
	private final TestUserRepository userRepo;
	private final ReservationRepository resRepo;
	private final PaymentRepository paymentRepository;
	
	private final ObjectMapper objectMapper;  // Jackson

	// ── 기존 list 메서드 그대로 남겨두세요 ─────────
//	    @GetMapping("/list")
//	    public String listInterviewers(Model model) {
//	        List<Users> intrs = userRepo.findByUserRole(Users.Role.intr);
//	        model.addAttribute("interviewers", intrs);
//	        return "reservation/list";
//	    }

	// ── 임시 테스트용 testlist 추가 ───────────────────
	@GetMapping("/testlist")
	public String testListInterviewers(Model model) {
		List<Users> interviewers = userRepo.findInterviewersNative();
		model.addAttribute("interviewers", interviewers);
		return "reservation/testlist";
	}

	// 예약 페이지 열기
	@GetMapping("/book")
	public String showBookPage(@RequestParam("intrId") String intrId, Model model, HttpSession session) {
		// (1) 예약하려는 면접관 ID
		model.addAttribute("intrId", intrId);
		// (2) 해당 면접관에 이미 예약된 날짜 목록 조회 (서비스 메서드)
		// yyyy-MM-dd 포맷의 String 리스트를 반환한다고 가정
		List<String> disabledDates = service.findReservedDatesByIntrId(intrId);
		model.addAttribute("disabledDates", disabledDates);
		// 날짜별 예약된 시간 가져오기
		Map<String, List<String>> slots = service.getReservedSlotsByIntrId(intrId);
		model.addAttribute("reservedSlots", slots);

		// 비활성화된 시간도 함께 조회해서 전달
		Map<String, List<String>> disabledSlots = service.getDisabledSlotsByIntrIdGrouped(intrId);
		model.addAttribute("disabledSlots", disabledSlots);
		// 로그인한 사용자 이름 보여주기
		// 사용자 정보 (session에서 Users 꺼내기)
		Users user = (Users) session.getAttribute("user");
		if (user != null) {
			model.addAttribute("userId", user.getUserId());
			model.addAttribute("userName", user.getUserName());
			model.addAttribute("userTel", user.getUserNum());
		} else {
			model.addAttribute("userName", "비로그인 사용자");
		}
		// (3) 뷰로 이동
		return "reservation/book";
	}

	// 예약 저장
	@PostMapping("/save")
	// public ResponseEntity<Integer> save
	public ResponseEntity<?> save(@RequestBody Reservation res, HttpSession session) {
		System.out.println(res.getIntrId());
		System.out.println(res.getReservedDate());
		System.out.println(res.getReservedTime());
		// 1) 로그인된 회원 ID

		Users user = (Users) session.getAttribute("user");
		if (user == null)
			return ResponseEntity.badRequest().build();

		String memId = user.getUserId();
		res.setMemId(memId);
		res.setResStatus("pending");// 상태저장

		// 2) 면접관의 비활성화된 시간인지 검사
		boolean disabled = service.isDisabled(res.getIntrId(), res.getReservedDate(), res.getReservedTime());
		if (disabled) {
			return ResponseEntity.badRequest().body("해당 시간은 면접관이 예약을 받을 수 없는 시간입니다.");
		}

		// 저장된 예약 엔티티 받아오기
		Reservation reservation = service.save(res);

		// 저장된 예약의 PK 반환!
		return ResponseEntity.ok(reservation.getResId());

	}

	// 예약 결과 페이지 (result.html)
	@GetMapping("/result")
	public String showResultPage(@RequestParam("resId") Long resId,
			@RequestParam(value = "error", required = false) String error, Model model, HttpSession session) {
		Reservation reservation = service.findById(resId);
		model.addAttribute("reservation", reservation); // ★ 중요
		model.addAttribute("resId", resId); // 생성된 예약 PK
		model.addAttribute("error", error); // 에러 메시지(선택)

		// 사용자 이름, 전화번호도 추가로 넘김
		Users user = (Users) session.getAttribute("user");
		if (user != null) {
			model.addAttribute("userId", user.getUserId());
			model.addAttribute("userTel", user.getUserNum());
			model.addAttribute("userName", user.getUserName());
		}

		return "reservation/result";

	}

	// 로그인된 회원의 “결제 전”/“결제 완료” 예약을 조회해서 View에 전달
	@GetMapping("/myReservations")
	public String myReservations(Model model, @AuthenticationPrincipal UserDetails userDetails // 인증된 사용자 정보 주입
			, HttpSession session) {
		String userId = (String) session.getAttribute("userId");
		if (userId == null)
			return "redirect:/loginmain";
		// UserDetails에서 회원 ID 추출 ───
		// String userId = userDetails.getUsername();

		// 서비스 호출: 결제 대기(미완료) 예약 목록 ───
		List<Reservation> pending = service.getPendingReservations(userId);
		// 서비스 호출: 결제 완료된 예약 목록 ───
		List<Reservation> paid = service.getPaidReservations(userId);

		// ⑦Model에 각각 담아서 JSP로 전달 ───
		model.addAttribute("pendingList", pending); // 결제 대기 중
		model.addAttribute("paidList", paid); // 결제 완료

		// 뷰 이름 반환 ( /WEB-INF/views/reservation/list.jsp ) ───
		return "reservation/mylist";
	}

	// 예약변경 폼 보여주기 (edit.html) ─────────────────
	@GetMapping("/edit")
	public String showEditForm(@RequestParam("resId") Long resId, Model model) {
		Reservation r = service.findById(resId);
		if (r == null) {
			model.addAttribute("error", "해당 예약을 찾을 수 없습니다.");
			return "reservation/result";
		}
		model.addAttribute("reservation", r);
		model.addAttribute("reservedSlots", service.getReservedSlotsByIntrId(r.getIntrId()));
		return "reservation/edit";
	}

	// 예약 변경
//	@GetMapping("/reschedule")
//	public String reschedule(@RequestParam("resId") Long resId, @RequestParam("reservedDate") String newDate,
//			@RequestParam("reservedTime") String newTime) {
//		// 서비스에 위임 (dirty-checking 으로 자동 UPDATE)
//		service.reschedule(resId, newDate, newTime);
//		// 변경 후 내 예약 목록으로 돌아가기
//		return "redirect:/reservation/mylist";
//	}
	@PostMapping("/reschedule")
    public ResponseEntity<Void> reschedule(
            @RequestParam("resId") Long resId,
            @RequestParam("newDate") String newDate,
            @RequestParam("newTime") String newTime
    ) {
        // 서비스에서 Dirty-checking으로 자동 저장
        service.reschedule(resId, newDate, newTime);
        // 성공했음을 200 OK로 반환
        return ResponseEntity.ok().build();
    }

	// 예약 취소
	@GetMapping("/cancel")
	public String cancel(@RequestParam("resId") Long resId, HttpSession session) {
		// 1. 세션에서 사용자 확인
		Users user = (Users) session.getAttribute("user");
		if (user == null) {
			return "redirect:/loginmain";
		}
		System.out.println("✅ user: " + user); // 👉 null이면 환불 안 됨

		String userId = user.getUserId();

		// 2. 예약 상태를 'cancelled'로 변경 (삭제 X)
		service.cancelReservation(resId); // 예약 상태 업데이트

		// 3. 해당 예약이 결제된 상태인지 확인
		boolean wasPaid = paymentService.isPaidReservation(resId);
		System.out.println("🔍 예약번호 " + resId + " 결제 여부 wasPaid = " + wasPaid);  
		// 결제된 상태였다면 결제 금액을 포인트로 환불
		if (wasPaid) {
		    // 1. 모든 결제 내역 중 paid 상태인 것만 추출
		    List<Payment> payments = paymentRepository.findByResId(resId).stream()
		        .filter(p -> p.getPayStatus() == Payment.PayStatus.paid)
		        .toList();

		    for (Payment payment : payments) {
		        System.out.println("🔍 [환불대상] " + payment.getPayType() + " | " + payment.getPayAmount());

		        if (payment.getPayType() == Payment.PayType.POINT) {
		            pointservice.refundPoint(payment.getUserId(), payment.getPayAmount().intValue());
		            // 🔥 상태 변경 추가
		            payment.setPayStatus(Payment.PayStatus.refunded);
		            paymentRepository.save(payment); // 꼭 저장해야 DB에 반영됨
		        } else if (payment.getPayType() == Payment.PayType.CARD) {
		        }
		    }
		}
		System.out.println("🧾 [예약 취소 요청]");
		System.out.println(" - 예약 ID: " + resId);
		System.out.println(" - 사용자 ID: " + userId);

		// 5. 마이페이지로 이동
		return "redirect:/reservation/mylist";
	}

	// 내 예약 목록
	@GetMapping("/mylist")
	 public String myList(Model model, HttpSession session) throws JsonProcessingException {

		Users user = (Users) session.getAttribute("user");
		System.out.println("user: " + user);
		if (user == null)
			return "redirect:/loginmain";

		String userId = user.getUserId();

		System.out.println("🧾 [세션 정보 확인]");
		System.out.println("🔸 userId   : " + userId);

		// 서비스에서 내 예약 목록(시간 내림차순) 가져오기
		List<Reservation> myResList = service.findReservationsByMemId(userId);
		

		// 면접관 ID → 이름 매핑 Map 만들기
		Map<String, String> intrNames = myResList.stream().map(Reservation::getIntrId).distinct()
				.filter(id -> id != null && !id.isBlank()).collect(java.util.stream.Collectors.toMap(id -> id, id -> {
					Users intr = userRepo.findByUserId(id).orElse(null);
					return intr != null ? intr.getUserName() : "(알 수 없음)";
				}));

		// 🔹 현재 포인트 조회
		int point = pointservice.getPoint(userId);
		

	    //  예약된 슬롯 정보(JSON) 직렬화

		    // 🔽 예약/차단 시간 통합 처리
		    String intrIdForSlots = myResList.isEmpty() ? userId : myResList.get(0).getIntrId();

		    Map<String, List<String>> reservedSlots = service.getReservedSlotsForInterviewer(intrIdForSlots);
		    Map<String, List<String>> disabledSlots = service.getDisabledSlotsByIntrIdGrouped(intrIdForSlots);

		    Map<String, Set<String>> merged = new LinkedHashMap<>();
		    reservedSlots.forEach((date, times) -> merged
		        .computeIfAbsent(date, k -> new LinkedHashSet<>())
		        .addAll(times));
		    disabledSlots.forEach((date, times) -> merged
		        .computeIfAbsent(date, k -> new LinkedHashSet<>())
		        .addAll(times));

		    // 정렬해서 JSON 변환
		    Map<String, List<String>> mergedSorted = merged.entrySet().stream()
		        .collect(Collectors.toMap(
		            Map.Entry::getKey,
		            e -> e.getValue().stream().sorted().collect(Collectors.toList()),
		            (a, b) -> b, LinkedHashMap::new));

		    String slotsJson = objectMapper.writeValueAsString(mergedSorted);

		 //각 예약에 해당하는 결제정보 주입
		    for (Reservation r : myResList) {
		        Payment pay = paymentService.findLatestPaidByResId(r.getResId()); // 최근 결제 정보
		        r.setPayment(pay); // Reservation 객체에 결제정보 주입 (Transient)
		    }
        model.addAttribute("reservedSlotsJson", slotsJson);
		model.addAttribute("point", point); // 뷰로 전달
		model.addAttribute("myReservations", myResList);
		model.addAttribute("userId", userId);
		model.addAttribute("intrNames", intrNames);
		model.addAttribute("userName", user.getUserName());

		 return "mypage/memschedule :: memschedule";
	}

	// 면접관 자신의 예약 일정(마이페이지) 확인용
	@GetMapping("/intrmypage")
	public String intrReservationList(HttpSession session, Model model,HttpServletRequest request) {

		// 세션에서 로그인된 사용자 가져오기
		Users user = (Users) session.getAttribute("user");
		if (user == null || !"intr".equals(user.getUserRole())) {
			return "redirect:/login"; // 로그인하지 않았거나 일반회원이면 로그인 페이지로
		}
		String userId = user.getUserId();

		// 서비스 호출해서 예약 목록 가져오기
		List<Reservation> reservations = service.getIntrReservations(user.getUserId());

		List<IntrDisabled> disabledList = service.getDisabledDatesByIntrId(user.getUserId());
		// 포인트 조회
		int point = pointservice.getPoint(userId);
		// 뷰로 전달
		model.addAttribute("disabledList", disabledList);
		model.addAttribute("reservations", reservations);
		model.addAttribute("user", user);
		model.addAttribute("users", user);
		model.addAttribute("point", point);
		// AJAX 요청이면 fragment만 반환
		if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
		    return "mypage/intrschedule :: fragment"; // ← fragment 이름 명시
		}
		return "mypage/intrMypage";
		
	}

	// 면접관 불가능날자 등록
	@PostMapping("/blockDate")
	public ResponseEntity<Object> blockDate(@RequestParam("date") String date, @RequestParam("time") String time, HttpSession session) {

		// 세션에서 로그인된 사용자 확인
		Users user = (Users) session.getAttribute("user");
		if (user == null || !"intr".equals(user.getUserRole())) {
			return ResponseEntity.status(401).build();
		}
		 String intrId = user.getUserId();
		 // 2. 중복 체크
	    boolean exists = service.isDisabled(intrId, date, time); // 서비스에 위임
	    if (exists) {
	        return ResponseEntity.status(409).body("duplicate"); // 409 Conflict
	    }
		// 서비스 호출 → 날짜+시간 비활성화로 저장
		service.saveDisabledDate(user.getUserId(), date, time);

		// 다시 마이페이지로 리다이렉트
		//return "redirect:/reservation/intrmypage";
		
		return ResponseEntity.ok("success"); // ✅ redirect NO!
		

	}
	
	// 비활성화된 날짜/시간 삭제
	@PostMapping("/deleteDisabled")
	public ResponseEntity<?> unblockDate(@RequestParam("id") Long id, HttpSession session) {
	    Users user = (Users) session.getAttribute("user");
	    if (user == null || !"intr".equals(user.getUserRole())) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
	    }

	    service.deleteDisabledTime(id);
	    return ResponseEntity.ok("success");
	    
	        }

}
