package com.shop.service;

import com.shop.dto.ItemSearchDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {
    
    @Value("${itemImgLocation}")// properties에 저장한 itemImgLocation 값을 불러와 변수에 저장
    private String itemImgLocation;
    
    private final ItemImgRepository itemImgRepository;
    
    private final FileService fileService;
    
    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{
        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName="";
        String imgUrl="";
        
        //파일 업로드
        if(!StringUtils.isEmpty(oriImgName)){
            //상품의 이미지를 등록했다면 저장할 경로와 파일의 이름, 파일의 바이트 배열을 파일 업로드 파라미터로 uploadFile 메소드 호출 결과를 imgName변수에 저장
            imgName=fileService.uploadFile(itemImgLocation,oriImgName,itemImgFile.getBytes());
            imgUrl="/images/item/" + imgName;//
        }
        
        //상품 이미지 정보 저장
        itemImg.updateItemImg(oriImgName,imgName,imgUrl);// 입력받은 상품 이미지 정보를 저장
        itemImgRepository.save(itemImg);// 업로드 했던 이미지 파일 원래 이름,로컬에 저장된 상품이미지 파일의 이름,업로드 결과 롴컬에 저장된 파일 불러오는 경로
    }

    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception{
        if(!itemImgFile.isEmpty()){
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId)
                    .orElseThrow(EntityNotFoundException::new);

            //기존 이미지 파일 삭제
            if(!StringUtils.isEmpty(savedItemImg.getImgName())) {
                fileService.deleteFile(itemImgLocation+"/"+
                        savedItemImg.getImgName());
            }

            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            String imgUrl = "/images/item/" + imgName;
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);
        }
    }

}
