package com.shop.entity;

import com.shop.dto.MemberFormDto;
import com.shop.repository.CartRepository;
import com.shop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")
class CartTest {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PersistenceContext
    EntityManager em;

    public Member createMember(){                                 // 회원 엔티티 생성하는 매소드
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail("test@test.com");
        memberFormDto.setPassword("1234");
        memberFormDto.setName("홍길동");
        memberFormDto.setAddress("서울시 마포구 합정동");
        return Member.createMember(memberFormDto,passwordEncoder);
    }

    @Test
    @DisplayName("장바구니 회원 엔티티 매핑 조회 테스트")
    public void findCartAndMemberTest(){
        Member member=createMember();
        memberRepository.save(member);

        Cart cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);

        em.flush();         //데이터를 저장 후 트랜잭션이 끝날 때 flush() 호출하여 데이터베이스에 반영,엔티티매니저로부터 강제로 flush()호출하여 데이터 베이스에 반영
        em.clear();         //엔티티가 없을 경우 데이터베이스 조회, 실제 데이터베이스에서 장바구니 엔티티를 가지고 올 때 회원 엔티티도 가지고 오는지 보기 위해 비워줌

        Cart savedCart= cartRepository.findById(cart.getId())        // 지정된 장바구니 엔티티를 조회
                .orElseThrow(EntityNotFoundException::new);
        assertEquals(savedCart.getMember().getId(),member.getId()); // 처음에 저장한 member엔티티의 id와 savedCart에 매핑된 member 엔티티의 id를 비교
    }


}