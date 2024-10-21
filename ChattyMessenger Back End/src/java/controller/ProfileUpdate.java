package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
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
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@MultipartConfig
@WebServlet(name = "ProfileUpdate", urlPatterns = {"/ProfileUpdate"})
public class ProfileUpdate extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();

        responseJson.addProperty("success", false);
        responseJson.addProperty("message", "Error occurred");

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        Part avatarImage = request.getPart("avatarImage");

        try {
            String mobile = request.getParameter("mobile");
            Session session = HibernateUtil.getSessionFactory().openSession();
            User user = (User) session.createCriteria(User.class)
                    .add(Restrictions.eq("mobile", mobile))
                    .uniqueResult();

            if (user != null) {
                // Update user details
                user.setFirst_name(firstName);
                user.setLast_name(lastName);

                if (avatarImage != null) {
                    // Handle avatar image upload
                    String serverPath = request.getServletContext().getRealPath("");
                    String userImagesPath = serverPath + File.separator + "User_Images";
                    File avatarImagePath = new File(userImagesPath);
                    if (!avatarImagePath.exists()) {
                        avatarImagePath.mkdirs();
                    }

                    File file = new File(avatarImagePath, mobile + ".png");
                    try (InputStream inputStream = avatarImage.getInputStream()) {
                        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                session.beginTransaction();
                session.update(user);
                session.getTransaction().commit();

                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Profile updated successfully!");
            } else {
                responseJson.addProperty("message", "User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("message", "Error: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
}
