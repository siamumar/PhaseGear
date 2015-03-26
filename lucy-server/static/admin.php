<?php
$con=mysqli_connect("localhost", "root", "56289086", "EmpData");
// Check connection
if (mysqli_connect_errno()) {
	echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

$result = mysqli_query($con,"SELECT * FROM mood");
//$result2 = mysqli_query($con,"SELECT * FROM return_items");

// Construct table
echo "<table cellpadding='5px'><tr><td>ID</td><td>UUID</td><td>Timestamp</td><td>Mood level</td><td>Activity level</td>";
while($row = mysqli_fetch_array($result)) {
	echo "<tr>";
	echo "<td>" . $row['_id'] . "</td>";
	echo "<td>" . $row['uuid'] . "</td>";
	echo "<td>" . $row['timestamp'] . "</td>";
	echo "<td>" . $row['moodlevel'] . "</td>";
	echo "<td>" . $row['activitylevel'] . "</td>";
	echo "</tr>";
}
echo "</tr></table>";
mysqli_close($con);
?> 