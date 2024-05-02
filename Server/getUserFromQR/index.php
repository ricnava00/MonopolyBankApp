<?php
/*In:
qr

Out:
user
*/
include "../api.php";
$qr=openssl_decrypt($qr,"AES-128-CBC",QR_PASSWORD);
if($qr===false)
{
	reterror("Il QR non è valido");
}
else
{
	$found=searchuserqr($qr);
	if($found!==false)
	{
		retarray(array('user'=>$found));
	}
	else
	{
		reterror("Nessun utente trovato per il QR");
	}
}
?>