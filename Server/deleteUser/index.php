<?php
/*
In:
user

Out: none
*/
include "../api.php";
if(!user_exists($user))
{
	reterror("Non puoi uccidere un fantasma");
	exit;
}
else
{
	if($session['users'][$user]['balance']>0)
	{
		reterror("Prima il conto dell'utente deve essere azzerato trasferendo i soldi a un altro utente o alla banca");
	}
	else
	{
		unset($session['users'][$user]);
		$session['graveyard'][]=$user;
		update();
		retok();
	}
}
?>