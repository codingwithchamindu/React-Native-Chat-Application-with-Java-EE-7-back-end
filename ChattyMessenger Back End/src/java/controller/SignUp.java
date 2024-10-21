package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Session;
import java.sql.Date;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

@MultipartConfig
@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseobject = new JsonObject();
        responseobject.addProperty("success", false);
        responseobject.addProperty("message", "Error");

        String mobile = request.getParameter("mobile");
        String fname = request.getParameter("firstName");
        String lname = request.getParameter("lastName");
        String password = request.getParameter("password");
        Part avatarImage = request.getPart("avatarImage");

        // Validate input
        if (fname.isEmpty()) {
            responseobject.addProperty("message", "Please enter your First Name");
        } else if (lname.isEmpty()) {
            responseobject.addProperty("message", "Please enter your Last Name");
        } else if (mobile.isEmpty()) {
            responseobject.addProperty("message", "Please Enter your mobile");
        } else if (!Validations.isMobileNumberValid(mobile)) {
            responseobject.addProperty("message", "Invalid mobile Number");
        } else if (password.isEmpty()) {
            responseobject.addProperty("message", "Please enter your password");
        } else if (!Validations.isPasswordValid(password)) {
            responseobject.addProperty("message", "Invalid password");
        } else {
            // Begin session
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            // Check if mobile number exists
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("mobile", mobile));

            if (!criteria.list().isEmpty()) {
                responseobject.addProperty("message", "Mobile number already exists");
            } else {
                // Proceed with registration
                User user = new User();
                user.setFirst_name(fname);
                user.setLast_name(lname);
                user.setMobile(mobile);
                user.setPassword(password);
                user.setRegistered_date_time(new Date(System.currentTimeMillis()));

                // Assign default user status
                User_Status user_Status = (User_Status) session.get(User_Status.class, 2);
                user.setUser_status_id(user_Status);

                // Save the user
                session.save(user);
                session.getTransaction().commit();

                // Handle avatar image if provided
                if (avatarImage != null && avatarImage.getSize() > 0) {
                    String serverPath = request.getServletContext().getRealPath("");
                    String newApplicationPath = serverPath.replace("build" + File.separator + "web", "web");
                    File avatarImagePath = new File(newApplicationPath + File.separator + "User_Images");
                    avatarImagePath.mkdir();

                    File file1 = new File(avatarImagePath, "" + mobile + ".png");
                    InputStream inputStream1 = avatarImage.getInputStream();
                    Files.copy(inputStream1, file1.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                responseobject.addProperty("success", true);
                responseobject.addProperty("message", "Registration Complete!");
            }

            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseobject));
    }
}
