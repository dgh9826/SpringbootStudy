package com.shop.repository;

import com.shop.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item,Long>, QuerydslPredicateExecutor<Item> {
    //itemName으로 상품 찾기
    List<Item> findByItemNm(String itemNm);

    //itemName Or itemDetail로 상품 찾기
    List<Item> findByItemNmOrItemDetail(String itemNm,String itemDetail);

    //특정 price 값 이하의 상품 찾기
    List<Item> findByPriceLessThan(Integer price);

    //특정 price 값 이하의 상품 내림차순으로 찾기
    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);

    //itemDetail을 파라미터로 받아 조회 => 특정 데이터베이스에 의존하지 않음
    // 즉, 데이터베이스가 변경되어도 어플리케이션은 영향을 받지 않는다.
    @Query("select i from Item i where i.itemDetail like %:itemDetail% order by i.price desc")
    List<Item> findByItemDetail(@Param("itemDetail") String itemDetail);

    //itemDetail을 기존 쿼리를 그대로 활용하여 조회 => 데이터베이스에 대해 독립적이라는 장점을 잃음
    @Query(value="select * from item i where i.item_detail like %:itemDetail% order by i.price desc", nativeQuery = true)
    List<Item> findByItemDetailByNative(@Param("itemDetail") String itemDetail);
}