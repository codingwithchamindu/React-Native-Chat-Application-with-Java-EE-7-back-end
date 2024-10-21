package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJsonObject = new JsonObject();

        responseJsonObject.addProperty("success", false);
        responseJsonObject.addProperty("message", "Error");

        JsonObject requestJsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String mobile = requestJsonObject.get("mobile").getAsString();
        String password = requestJsonObject.get("password").getAsString();

        if (mobile.isEmpty()) {
            //mobile number is blank
            responseJsonObject.addProperty("message", "Please Enter your mobile");
        } else if (!Validations.isMobileNumberValid(mobile)) {
            //Invalid mobile number
            responseJsonObject.addProperty("message", "Invalid mobile Number");

        } else if (password.isEmpty()) {
            responseJsonObject.addProperty("message", "Please enter your password");
        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("mobile", mobile));
            criteria.add(Restrictions.eq("password", password));

            if (!criteria.list().isEmpty()) {
                //user found
                User user = (User) criteria.uniqueResult();

                responseJsonObject.addProperty("success", true);

                responseJsonObject.addProperty("message", "Sign In Success!");
                responseJsonObject.add("user", gson.toJsonTree(user));
            } else {
                responseJsonObject.addProperty("message", "Invalid Detalis plese check this?!");

            }
            session.close();
        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJsonObject));
    }

}
