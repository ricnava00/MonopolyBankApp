<?php
/*
In:
verify

Out: none
*/
include "../api.php";
if($verify)
{
	pay($session['unverified']['from'],$session['unverified']['to'],$session['unverified']['value']);
}
unset($session['unverified']);
update();
retok();
?>
