package cc.mrbird.febs.system.service.impl;

import cc.mrbird.febs.common.utils.AddressUtil;
import cc.mrbird.febs.common.utils.HttpContextUtil;
import cc.mrbird.febs.common.utils.IPUtil;
import cc.mrbird.febs.system.dao.LoginLogMapper;
import cc.mrbird.febs.system.domain.LoginLog;
import cc.mrbird.febs.system.service.LoginLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @Transactional(propagation=Propagation.REQUIRED)
 * 如果有事务, 那么加入事务, 没有的话新建一个(默认情况下)
 * @Transactional(propagation=Propagation.NOT_SUPPORTED)
 * 容器不为这个方法开启事务
 * @Transactional(propagation=Propagation.REQUIRES_NEW)
 * 不管是否存在事务, 都创建一个新的事务, 原来的挂起, 新的执行完毕, 继续执行老的事务
 * @Transactional(propagation=Propagation.MANDATORY)
 * 必须在一个已有的事务中执行, 否则抛出异常
 * @Transactional(propagation=Propagation.NEVER)
 * 必须在一个没有的事务中执行, 否则抛出异常(与Propagation.MANDATORY相反)
 * @Transactional(propagation=Propagation.SUPPORTS)
 * 如果其他bean调用这个方法, 在其他bean中声明事务, 那就用事务.如果其他bean没有声明事务, 那就不用事务.
 *
 * @Transactional(timeout=30) //默认是30秒
 *
 * @Transactional(isolation = Isolation.READ_UNCOMMITTED)
 * 读取未提交数据(会出现脏读, 不可重复读) 基本不使用
 * @Transactional(isolation = Isolation.READ_COMMITTED)
 * 读取已提交数据(会出现不可重复读和幻读)
 * @Transactional(isolation = Isolation.REPEATABLE_READ)
 * 可重复读(会出现幻读)
 * @Transactional(isolation = Isolation.SERIALIZABLE)
 * 串行化
 *
 * MYSQL: 默认为REPEATABLE_READ级别
 * SQLSERVER: 默认为READ_COMMITTED
 */
@Service("loginLogService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    @Override
    @Transactional
    public void saveLoginLog(LoginLog loginLog) {
        loginLog.setLoginTime(new Date());
        HttpServletRequest request = HttpContextUtil.getHttpServletRequest();
        String ip = IPUtil.getIpAddr(request);
        loginLog.setIp(ip);
        loginLog.setLocation(AddressUtil.getCityInfo(ip));
        this.save(loginLog);
    }
}
