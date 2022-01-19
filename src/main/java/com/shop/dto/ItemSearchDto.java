package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class ItemSearchDto {
    /*  현재 시간과 상품 등록일을 비교해서 상품 데이터를 조회
        all:등록일 전체 / 1d:최근 하루 / 1w:최근 일주 / 1m: 한달 / 6m: 6달
    */
    private String searchDateType;
    //상품의 판매상태를 기준으로 상품 데이터 조회
    private ItemSellStatus searchSellStatus;
    //상품을 조회할 때 어떤 유형으로 조회 할지 선택
    //itemNm : 상품명 , createdBy:상품 등록자 아이디
    private String searchBy;
    //조회할 검색어 저장할 변수
    private String searchQuery="";

}
