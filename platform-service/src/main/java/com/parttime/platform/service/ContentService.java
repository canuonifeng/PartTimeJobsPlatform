package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.BannerCmd;
import com.parttime.platform.pojo.cmd.HotRecommendationCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.ServiceFeeRateCmd;
import com.parttime.platform.pojo.vo.BannerVO;
import com.parttime.platform.pojo.vo.HotRecommendationVO;
import com.parttime.platform.pojo.vo.ServiceFeeRateVO;

import java.util.List;

public interface ContentService {

    List<BannerVO> banners();

    void createBanner(BannerCmd cmd);

    void updateBanner(BannerCmd cmd);

    void deleteBanner(IdCmd cmd);

    List<HotRecommendationVO> hotRecommendations();

    void addHotRecommendation(HotRecommendationCmd cmd);

    void removeHotRecommendation(IdCmd cmd);

    List<ServiceFeeRateVO> serviceFeeRates();

    void updateServiceFeeRate(ServiceFeeRateCmd cmd);

    List<String> regions();
}
