<?php
$kill=file_get_contents("kill");
var_export(!$kill);
file_put_contents("kill",!$kill);
?>