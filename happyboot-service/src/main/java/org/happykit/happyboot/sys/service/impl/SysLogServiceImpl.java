package org.happykit.happyboot.sys.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.happykit.happyboot.log.annotation.LogType;
import org.happykit.happyboot.log.model.Log;
import org.happykit.happyboot.sys.model.query.SysLogPageQuery;
import org.happykit.happyboot.sys.service.SysLogService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 日志
 *
 * @author chen.xudong
 * @version 1.0
 * @since 2020/6/9
 */
@Slf4j
@Service
public class SysLogServiceImpl implements SysLogService {

//    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void saveLog(Log log, LogType logType) {
        mongoTemplate.save(log, logType.getCollectName());
    }

    @Override
    public Page<Log> selectPageList(SysLogPageQuery query, LogType logType) {
        Long pageNo = query.getPageNo();
        Long pageSize = query.getPageSize();
        String username = query.getUsername();
        String collectName = logType.getCollectName();

        Query q = new Query();
        if (StringUtils.isNotBlank(username)) {
            q.addCriteria(Criteria.where("requestUser").is(query.getUsername()));
        }
        long total = mongoTemplate.count(q, collectName);

        q.skip(pageSize * (pageNo - 1));
        q.limit(pageSize.intValue());
        q.with(Sort.by(Sort.Order.desc("requestTime")));
        List<Log> list = mongoTemplate.find(q, Log.class, collectName);

        Page<Log> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(pageSize);
        page.setTotal(total);
        page.setRecords(list);
        return page;
    }

    @Override
    public void clear(LogType logType) {
        mongoTemplate.remove(new Query(), logType.getCollectName());
    }
}
