<?php
/*
In:
from
to

Out: none
*/
include "../api.php";
if(user_exists($to)&&$to==$from)
{
	retok();
	exit;
}
if(empty($to))
{
	reterror("Non è possibile impostare un nome utente vuoto");
}
else if(in_array($to,$reserved))
{
	reterror("Il nome ".$to." è riservato");
}
else
{
	if(user_exists($to))
	{
		reterror("Il nome utente specificato esiste già");
	}
	else
	{
		if(user_exists($from))
		{
			$session['users'][$to]=$session['users'][$from];
			unset($session['users'][$from]);
		}
		else
		{
			$session['users'][$to]=newuser();
		}
		update();
		retok();
	}
}
?>