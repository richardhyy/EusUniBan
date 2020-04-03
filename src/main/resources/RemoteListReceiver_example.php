<?php
//=======================================================================================================
// UniBanCopy connection password
//=======================================================================================================
$password = "unibancopy";

if (!empty($_POST["secret"])) {
    if ($_POST["secret"] != $password) {
        die("Access denied.");
    }
}
else {
  die("Empty password.");
}


$content = $_POST['list'];
$file = fopen("get", "w") or die("Unable to open file!");
fwrite($file, $content);
fclose($file);
die(200);