<?php
/*
In:
qr
user

Out: none
*/

//Possibile errore di carta già usata da gestire da qui
include "../api.php";
$qr=openssl_decrypt($qr,"AES-128-CBC",QR_PASSWORD);
if($qr===false)
{
	reterror("Il QR non è valido");
}
else
{
	if(user_exists($user))
	{
		$found=searchuserqr($qr);
		if($found!==false)
		{
			unset($session['users'][$found]['qr']);
		}
		$session['users'][$user]['qr']=$qr;
		update();
		retok();
	}
	else
	{
		reterror("In qualche modo sei riuscito a selezionare un utente che non esiste");
	}
}
?>