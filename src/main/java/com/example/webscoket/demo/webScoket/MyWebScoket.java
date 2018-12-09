package com.example.webscoket.demo.webScoket;

import com.example.webscoket.demo.entity.SocketEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by 自由翱翔峰 on 2018/12/7 16:09
 */

/**
 * 虽然@component默认是单例模式的，但在spring boot 中还是会为每一个webscoket连接初始化一个bean，所以这里使用一个静态的set保存spring boot创建的bean--MyWebscoket
 */
@ServerEndpoint ( value="/websocket/{name}" )
@Component
public class MyWebScoket {
    //用来存储每个客户端对应的MyWebScoket对象
    private static CopyOnWriteArraySet<MyWebScoket> webScoketset = new CopyOnWriteArraySet <MyWebScoket> (  );
    //用来记录sessionid和session之间的绑定关系。
    private static Map<String,Session> map = new HashMap <String,Session> (  );
    private Session session;//当前会话的session
    private String name;
    /**
     * 成功建立连接调用的方法
     */
    @OnOpen
    public void onOpen( Session session, @PathParam  ( "name" ) String name){
        this.session = session;
        this.name = name;
        map.put ( session.getId (),session );
        webScoketset.add ( this );//加入set中
        this.session.getAsyncRemote ().sendText ( name+"上线了"+"我的频道号是"+session.getId ()+",当前连接人数为："+webScoketset.size () );
    }

    /**
     * 连接关闭调用的方法
     * @param session
     */
    @OnClose
    public void onClose(Session session){

        webScoketset.remove ( this );//加入set中
        map.remove ( session.getId () );
    }
    @OnError
    public void onError(Session session,Throwable error){
        System.out.println("发生错误");
        error.printStackTrace ();
    }

    /**
     * 收到客户消息后调用的方法
     * @param session
     */
    @OnMessage
    public void OnMessage(String message,Session session, @PathParam  ( "name" ) String name) throws IOException {
        //message不是普通的string,而是我们定义的SocketEntity,json字符串
        SocketEntity socketEntity = new ObjectMapper (  ).readValue ( message,SocketEntity.class );
        //单聊
         if (socketEntity.getType ()==1){
             //单聊：需要找到发送者和接收者即可
             socketEntity.setFromUser ( session.getId () );//发送者
//             socketEntity.setToUser ( toUser );//这个有客户端进行设置
             Session fromsession = map.get ( socketEntity.getFromUser () );
             Session tosession = map.get ( socketEntity.getToUser ());
             if (tosession!=null){
                 fromsession.getAsyncRemote ().sendText ( name+":" +socketEntity.getMessage ());//发送消息
                 tosession.getAsyncRemote ().sendText ( name+":" +socketEntity.getMessage ());//发送消息
             }else {
                 fromsession.getAsyncRemote ().sendText ( "系统消息:对方不在线或者您输入的频道号有误");//发送消息
             }
         }else {
             //   广播群发给每一个客户端
             broadcast(socketEntity,name);
         }

    }

    /**
     * 群发消息
     * @param socketEntity
     */
    private void broadcast(SocketEntity socketEntity ,String name) {
        for (MyWebScoket myWebScoket:webScoketset){
            //发送消息
            myWebScoket.session.getAsyncRemote ().sendText (name+":"+socketEntity.getMessage ());
        }
    }


}
