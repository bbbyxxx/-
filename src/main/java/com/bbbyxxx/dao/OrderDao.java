package com.bbbyxxx.dao;

import com.bbbyxxx.domain.MiaoshaOrder;
import com.bbbyxxx.domain.OrderInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {
    @Select("select * from miaosha_order where user_id = #{userId} and goods_id = #{goodsId}")
    public MiaoshaOrder getMiaoShaOrderByUserIdGoodsId(@Param("userId") Long userId, @Param("goodsId") long goodsId);

    @Insert("insert into order_info(user_id,goods_id,goods_name,goods_count,goods_price,order_channel,status,create_date)"+
            "values(#{userId},#{goodsId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{status},#{createDate})")
    /*
    keyColumn:数据库的列
    keyProperty：domain中有这个列对应的属性列
    resultType：返回值类型
     */
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = long.class,before = false,statement = "select last_insert_id()")//获取返回值
    public long insert(OrderInfo orderInfo);

    @Insert("insert into miaosha_order(user_id,goods_id,order_id)values(#{userId},#{goodsId},#{orderId})")
    public int insertMiaoshaOrdder(MiaoshaOrder miaoshaOrder);

    @Select("select * from order_info where id = #{orderId}")
    public OrderInfo getOrderById(@Param("orderId") long orderId);

    @Delete("delete from order_info")
    public void deleteOrders();

    @Delete("delete from miaosha_order")
    public void deleteMiaoshaOrders();
}
