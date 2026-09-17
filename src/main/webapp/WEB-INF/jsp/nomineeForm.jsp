<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Nominee Profile & Evidence Upload</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .form-container { max-width: 500px; border: 1px solid #ccc; padding: 20px; border-radius: 5px; }
        .form-group { margin-bottom: 15px; }
        label { font-weight: bold; display: block; margin-bottom: 5px; }
        input, textarea { width: 100%; padding: 8px; }
        button { padding: 10px 15px; background-color: #28a745; color: white; border: none; cursor: pointer; }
    </style>
</head>
<body>

<h2>Submit Nominee Profile & Evidence</h2>

<div class="form-container">
    <!--
        ACTION: This form will send data to our 'NomineeController' Servlet.
        ENCTYPE: 'multipart/form-data' is MUST because we are uploading files!
    -->
    <form action="/nominee/submit" method="POST" enctype="multipart/form-data">

        <div class="form-group">
            <label>Full Name:</label>
            <input type="text" name="fullName" required>
        </div>

        <div class="form-group">
            <label>Biography:</label>
            <textarea name="biography" rows="4" required></textarea>
        </div>

        <div class="form-group">
            <label>Company Name:</label>
            <input type="text" name="companyName" required>
        </div>

        <div class="form-group">
            <label>Upload Evidence (Certificates/Letters):</label>
            <!-- Validating at the UI level to only accept PDF or PNG -->
            <input type="file" name="evidenceFile" accept=".pdf, .png" required>
            <small style="color:red;">* Only PDF or PNG allowed</small>
        </div>

        <button type="submit">Submit Profile</button>
    </form>
</div>

</body>
</html>