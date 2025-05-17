package models;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class BookingServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userId = request.getParameter("userId");
        File file = new File("data/bookings.txt");
        Map<String, Integer> bookings = new LinkedHashMap<>();

        // Read existing bookings
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    bookings.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
            reader.close();
        }

        // Update booking count
        int currentRides = bookings.getOrDefault(userId, 0);
        currentRides += 1;
        bookings.put(userId, currentRides);

        // Save updated data
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (Map.Entry<String, Integer> entry : bookings.entrySet()) {
            writer.write(entry.getKey() + "," + entry.getValue());
            writer.newLine();
        }
        writer.close();

        // Send response
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h2>Booking Successful!</h2>");
        out.println("<p>User ID: " + userId + "</p>");
        out.println("<p>Total Rides: " + currentRides + "</p>");
        out.println("<a href='booking.jsp'>Back</a>");
        out.println("</body></html>");
    }
}
