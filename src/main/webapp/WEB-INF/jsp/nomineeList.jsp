<%@ page import="java.util.List" %>
<%@ page import="com.pation.awardnomination.nominee.Nominee" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Nominee Profiles</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f9f9f9;}
        table { width: 100%; border-collapse: collapse; background: white; box-shadow: 2px 2px 10px rgba(0,0,0,0.1);}
        th, td { padding: 10px; border: 1px solid #ddd; text-align: left; }
        th { background-color: #007bff; color: white; }
        .status-draft { color: orange; font-weight: bold; }
    </style>
</head>
<body>

<h2>Registered Nominees & Status</h2>

<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Full Name</th>
        <th>Company</th>
        <th>Evidence File</th>
        <th>Current Status</th>
    </tr>
    </thead>
    <tbody>
    <%
        // Getting the Java Collection (List) passed from the Controller
        List<Nominee> nominees = (List<Nominee>) request.getAttribute("nominees");

        // If there are nominees, loop through them and display
        if(nominees != null && !nominees.isEmpty()) {
            for(Nominee n : nominees) {
    %>
    <tr>
        <!-- ENCAPSULATION: Using public Getters to read private data safely -->
        <td><%= n.getId() %></td>
        <td><%= n.getFullName() %></td>
        <td><%= n.getCompanyName() %></td>
        <td><%= n.getEvidenceFileName() %></td>
        <td class="status-draft"><%= n.getStatus() %></td>
    </tr>
    <%
        }
    } else {
    %>
    <tr>
        <td colspan="5" style="text-align: center;">No nominees found in the database.</td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>

<br>
<a href="/nominee/form" style="padding: 10px 15px; background-color: #28a745; color: white; text-decoration: none; border-radius: 3px;"> + Add New Nominee</a>

</body>
</html>