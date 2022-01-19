package com.shop.entity;

import com.shop.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
@Getter @Setter
public class Order extends BaseEntity{
    @Id @GeneratedValue
    @Column(name="order_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;
    
    private LocalDateTime orderDate; //주문일
    
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; //주문 상태
                                                                //영속성 상태 변화를 자식 엔티티에 모두 전이하는 Cascade TypeAll 설정
    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.LAZY)
    //OrderItem 엔티티와 일대다 매핑 왜래키가 order_item 테이블에 있으므로 주인은 OrderItem 엔티티

    private List<OrderItem> orderItems = new ArrayList<>(); //하나의 주문이 여러개의 주문상품을 갖으므로 List 사용

    //orderItems에는 주문 상품 정보들을 담아줌 orderItem객체를 order객체의 orderItems에 추가
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);//orderItem객체에도 order 객체 생성
    }

    public static Order createOrder(Member member, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setMember(member);// 상품을 주문한 회원의 정보 세팅
        //상품 페이지에서는 1개의 상품을 주문하지만 장바구니 페에지에서는 한번에 여러개의 상품을 주문 할 수 있음 따라서 여러개의 주문 상품을
        //담을 수 있도록 리스트 형태로 파라미터 값을 받으려 주문 객체에 orderitem 객체를 추가
        for(OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
        }

        order.setOrderStatus(OrderStatus.ORDER);// 주문상태를ORDER로 세팅
        order.setOrderDate(LocalDateTime.now());// 현재 시간으로 주문 시간 세팅
        return order;
    }
    // 총 주문 금액을 구하는 메소드
    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    public void cancelOrder(){
        this.orderStatus = OrderStatus.CANCEL;

        for(OrderItem orderItem : orderItems){
            orderItem.cancel();
        }
    }

}
