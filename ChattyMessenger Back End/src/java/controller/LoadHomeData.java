package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();

        responseJson.addProperty("success", false);
        responseJson.addProperty("message", "Unable to process your request");

        try {

            String user_id = request.getParameter("id");

            Session session = HibernateUtil.getSessionFactory().openSession();

            User user = (User) session.get(User.class, Integer.parseInt(user_id));
            //user (online) 1 status Update

            User_Status user_Status = (User_Status) session.get(User_Status.class, 1);

            user.setUser_status_id(user_Status);
            session.update(user);

            //get oTHER uSER
            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.ne("id", user.getId()));

            List<User> userList = criteria1.list();
            //remove password

            //get Other user one by one
            JsonArray jsonChatItemArray = new JsonArray();
            for (User OtherUser : userList) {

                //get Last convercation
                Criteria criteria2 = session.createCriteria(Chat.class);
                criteria2.add(Restrictions.or(
                        Restrictions.and(
                                Restrictions.eq("from_user", user),
                                Restrictions.eq("to_user", OtherUser)
                        ),
                        Restrictions.and(
                                Restrictions.eq("from_user", OtherUser),
                                Restrictions.eq("to_user", user)
                        )
                ));
                criteria2.addOrder(Order.desc("id"));
                criteria2.setMaxResults(2);

                //create chat item to send font-end data
                JsonObject chatItem = new JsonObject();
                chatItem.addProperty("other_user_id", OtherUser.getId());
                chatItem.addProperty("other_user_mobile", OtherUser.getMobile());

                chatItem.addProperty("other_user_name", OtherUser.getFirst_name() + " " + OtherUser.getLast_name());
                chatItem.addProperty("other_user_status", OtherUser.getUser_status_id().getId());

                //chaeck aveter image
                String servrPath = request.getServletContext().getRealPath("");
                String otherUserAvaterImagePath = servrPath + File.separator + "User_Images" + File.separator + OtherUser.getMobile() + ".png";
                File file = new File(otherUserAvaterImagePath);

                if (file.exists()) {
                    //aveter image found
                    chatItem.addProperty("avaterImageFound", true);
                } else {
                    //aveter image not found
                    chatItem.addProperty("avaterImageFound", false);
                    chatItem.addProperty("other_user_aveter_leters", OtherUser.getFirst_name().charAt(0) + " " + OtherUser.getLast_name().charAt(0));

                }
                //get chat List
                List<Chat> chatList = criteria2.list();
                SimpleDateFormat dataeFromates = new SimpleDateFormat("yyy,MM,dd hh:ss a");

                if (criteria2.list().isEmpty()) {
//                    no chat
                    chatItem.addProperty("message", "Start New Conversation.");
                    chatItem.addProperty("dateTime", dataeFromates.format(user.getRegistered_date_time()));

                    chatItem.addProperty("chat_status_id", 1);
                } else {
                    //found chat
                    chatItem.addProperty("message", chatList.get(0).getMessage());
                    chatItem.addProperty("dateTime", dataeFromates.format(chatList.get(0).getDate_time()));
                    chatItem.addProperty("chat_status_id", chatList.get(0).getChat_status().getId());
                }

                jsonChatItemArray.add(chatItem);

            }

            //user Chat Lists
            //user Send
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Success");
            responseJson.addProperty("jsonChatItemArray", gson.toJson(jsonChatItemArray));
            session.beginTransaction().commit();
            session.close();

        } catch (Exception e) {

        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));

    }

}
