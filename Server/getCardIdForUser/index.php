<?php
/*
In:
user

Out:
user
cardId
*/

//Possibile errore di carta già usata da gestire da qui
include "../api.php";
if(user_exists($user))
{
	$cardid=password_hash($user,PASSWORD_BCRYPT);
	$session['users'][$user]['cardId']=$cardid;
	update();
	retarray(array('user'=>$user,'cardId'=>$cardid));
}
else
{
	reterror("In qualche modo sei riuscito a selezionare un utente che non esiste");
}
?>