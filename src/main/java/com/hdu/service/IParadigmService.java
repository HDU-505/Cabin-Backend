package com.hdu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hdu.entity.Paradigm;
import com.hdu.entity.SearchForm;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author DZL
 * @since 2024-05-28
 */
public interface IParadigmService extends IService<Paradigm> {

    boolean deleteByIds(List<String> ids);
    void getAllParadigm(Page<Paradigm> page, SearchForm searchForm);
    boolean uploadParadigm(Paradigm paradigm);

    boolean uploadCoverFile(MultipartFile file, String type);

    Object getParadigmFile(String url);

    boolean sendParadigmToTouchScreen(String id);
}
