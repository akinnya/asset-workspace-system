package com.xxr.lingtuthinktank.service.favorite.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.mapper.favorite.FavoriteMapper;
import com.xxr.lingtuthinktank.model.entity.favorite.Favorite;
import com.xxr.lingtuthinktank.service.favorite.FavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
        implements FavoriteService {
}
