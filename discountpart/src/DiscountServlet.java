import discount.DiscountManager;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

public class DiscountServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userId = request.getParameter("userId");

        DiscountManager manager = new DiscountManager();
        double discount = manager.calculateDiscount(userId);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h2>Discount Summary</h2>");
        out.println("<p>User ID: " + userId + "</p>");
        out.println("<p>Your Total Discount: " + discount + "%</p>");
        out.println("</body></html>");
    }
}
