package com.hdu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hdu.entity.Experiment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hdu.entity.SearchForm;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author DZL
 * @since 2024-05-28
 */
public interface IExperimentService extends IService<Experiment> {

    void getAllExperiments(Page<Experiment> page, SearchForm searchForm);

    String createExperiment(Experiment experiment);

    boolean startExperiment(String experimentId);

    boolean endExperiment(String experimentId);

    byte[] getDataZip(String id);

    boolean delExperimentByIds(List<String> ids);
}
