<?php
/*
In:
user

Out:
array(array(user,balance,change))
*/
include "../api.php";
if(!user_exists($user))
{
	reterror("Utente \"".$user."\" non trovato");
}
else
{
	foreach($session['users'] as $usr=>$cont)
	{
		$change=$session['change'][$usr];
		if($change>0)
		{
			$change="+".$change;
		}
		$balance[]=array('user'=>$usr,'balance'=>$cont['balance'],'change'=>$change?:0);
	}
	uasort($balance,function($a,$b)
	{
		if($a['balance']==$b['balance'])
		{
			return strcmp($a['user'],$b['user']);
		}
		return ($a['balance'] > $b['balance']) ? -1 : 1;
	});
	$change=$session['change'][$user];
	if($change>0)
	{
		$change="+".$change;
	}
	array_unshift($balance,array('user'=>$user,'balance'=>$session['users'][$user]['balance'],'change'=>$change ?: 0));
	/*
	echo "<pre>";
	var_export($balance);
	echo "</pre>";
	*/
	retarray($balance);
}
?>
