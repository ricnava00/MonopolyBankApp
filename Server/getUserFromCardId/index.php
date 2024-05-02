<?php
/*In:
cardId

Out:
user
*/
include "../api.php";
$found=searchusercard($cardId);
if($found!==false)
{
	retarray(array('user'=>$found));
}
else
{
	reterror("Nessun utente trovato per la carta");
}
?>