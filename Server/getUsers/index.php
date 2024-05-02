<?php
/*
In:
user
isAdmin

Out:
array(string)
*/
include "../api.php";
if($isAdmin)
{
	retarray(array_merge(array("Banca","Tutti"),array_keys($session['users'])));
}
else
{
	retarray(array_values(array_diff(array_keys($session['users']),array($user))));
}
?>