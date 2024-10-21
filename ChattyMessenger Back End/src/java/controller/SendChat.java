package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.Chat_Status;
import entity.User;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;

/**
 *
 * @author Manujaya
 */
@WebServlet(name = "SendChat", urlPatterns = {"/SendChat"})
public class SendChat extends HttpServlet {

    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    // SendChat?logged_user_id=1&other_user_id=2&message=Hello
    Gson gson = new Gson();
    JsonObject responseJson = new JsonObject();
    responseJson.addProperty("success", false);
    
    Session session = HibernateUtil.getSessionFactory().openSession();
    
    // Get parameters and trim whitespace
    String logged_user_id = request.getParameter("logged_user_id");
    String other_user_id = request.getParameter("other_user_id");
    String message = request.getParameter("message");
    
    try {
        // Trim and parse the user IDs
        int senderId = Integer.parseInt(logged_user_id.trim());
        int receiverId = Integer.parseInt(other_user_id.trim());

        // Get logged_user and other_user
        User logged_user = (User) session.get(User.class, senderId);
        System.out.println(logged_user_id);

        User other_user = (User) session.get(User.class, receiverId);
        System.out.println(other_user_id);

        // Save chat
        Chat chat = new Chat();
        
        // Get chat status 2 = unseen
        Chat_Status chat_Status = (Chat_Status) session.get(Chat_Status.class, 2);
        chat.setChat_status(chat_Status);
        
        chat.setDate_time(new Date());
        chat.setFrom_user(logged_user);
        chat.setTo_user(other_user);
        chat.setMessage(message);
        
        // Begin transaction and save in db
        session.beginTransaction();
        session.save(chat);
        session.getTransaction().commit();
        responseJson.addProperty("success", true);

    } catch (NumberFormatException e) {
        System.err.println("Error parsing user IDs: " + e.getMessage());
    } catch (Exception e) {
        // Handle other exceptions (optional)
        e.printStackTrace();
    } finally {
        // Ensure the session is closed to prevent resource leaks
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    // Send response
    response.setContentType("application/json");
    response.getWriter().write(gson.toJson(responseJson));
}

    
}
