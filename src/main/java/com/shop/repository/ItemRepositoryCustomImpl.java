package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import com.shop.entity.QItemImg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
//ItemrepositoryCustom 상속
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{
    //동적 쿼리 생성하기 위해 JPAQueryFactory 사용
    private JPAQueryFactory queryFactory;
    //JpaQueryFactory의 생성자로 EntityManager 객체 넣어줌
    public ItemRepositoryCustomImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }
    //상품 판매 조건이 전체(null)일 경우 null 리턴, 결과값이 null이면 where절에서 해당 조건은 무시 
    //상품 판매 조건이 null이 아니라 판매중 or 품절 상태라면 해당 조건의 상품만 조회
    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus){
        //
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
        
    }
    //searchDataType 값에 따라 dateTime 의 값을 이전 시간의 값으로 세팅 후 해당 시간 이후로 등록된 상품만 조회
    //ex) searchDataType값이 "1m"인 경우 dateTime의 시간을 한달전으로 세팅후 최근 한달동안 등록된 상품만 조회하다록 if문에서 반환
    private BooleanExpression regDtsAfter(String searchDateType){  

        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all", searchDateType) || searchDateType == null){
            return null;
        } else if(StringUtils.equals("1d", searchDateType)){
            dateTime = dateTime.minusDays(1);
        } else if(StringUtils.equals("1w", searchDateType)){
            dateTime = dateTime.minusWeeks(1);
        } else if(StringUtils.equals("1m", searchDateType)){
            dateTime = dateTime.minusMonths(1);
        } else if(StringUtils.equals("6m", searchDateType)){
            dateTime = dateTime.minusMonths(6);
        }

        return QItem.item.regTime.after(dateTime);
    }
    //searchBy의 값에  따라서 상품명에 검색어를 포함하고있는 상품 또는 상품 생성자의 아이디에 검색어를 포함하고 있는 상품을 조회하도록 조건값을 반환
    private BooleanExpression searchByLike(String searchBy, String searchQuery){

        if(StringUtils.equals("itemNm", searchBy)){
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        } else if(StringUtils.equals("createdBy", searchBy)){
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }

        return null;
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        //queryFactory를 이용해서 쿼리를 생성
        //쿼리문을 직접 작성할 때의 형태와 문법 비슷
        QueryResults<Item> results = queryFactory
                .selectFrom(QItem.item)
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(),
                                itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults(); // 조회 대상 리스트 및 전체 개수를 포함하는 QueryResults 반환

        List<Item> content = results.getResults();
        long total = results.getTotal();
        //
        return new PageImpl<>(content, pageable, total);    //조회한 데이터를 Page클래스의 PageImpl객체로 반환
    }
    //검색어가 null이 아니면 상품명에 해당 검색어가 포함되는 상품을 조회하는 조건을 반환
    private BooleanExpression itemNmLike(String searchQuery){
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
                .select(
        //QMainItemDto의 생성자에 반환할 값들을 넣어줌
        //QueryProjection을 사용하면 DTO로 바로 조회가 가능, 엔티티 조회 후 DTO로 변환하는 과정을 줄일 수 있음
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price)
                )
                .from(itemImg)
                .join(itemImg.item, item)   //itemImg와 item 내부 조인
                .where(itemImg.repimgYn.eq("Y")) //상품 이미지는 대표 상품 이미지만
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

}