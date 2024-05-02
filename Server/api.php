<?php
define("INTBALANCE",1500);
define("INACTIVITY_TIMEOUT",3600);
define("QR_PASSWORD","MonopolyQR");

chdir(__DIR__);

function update()
{
	global $session;
	file_put_contents("session",json_encode($session));
}
function retok()
{
	retarray(array("^_^"=>"^_^"));
}
function reterror($err)
{
	echo json_encode(array("error"=>true,"description"=>$err), JSON_FORCE_OBJECT);
}
function retarray($arr)
{
	echo json_encode($arr, JSON_FORCE_OBJECT);
}
function makeusercard()
{
	global $session;
	foreach($session['users'] as $usr=>$cont)
	{
		$ret[$usr]=$cont['cardId'];
	}
	return($ret);
}
function makeuserqr()
{
	global $session;
	foreach($session['users'] as $usr=>$cont)
	{
		$ret[$usr]=$cont['qr'];
	}
	return($ret);
}
function makeuserid()
{
	global $session;
	foreach($session['users'] as $usr=>$cont)
	{
		$ret[$usr]=$cont['id'];
	}
	return($ret);
}
function searchusercard($cardId)
{
	$usercard=makeusercard();
	return(array_search($cardId,$usercard));
}
function searchuserqr($qr)
{
	$userqr=makeuserqr();
	return(array_search($qr,$userqr));
}
function pay($from,$to,$value)
{
	global $session;
	if($from=="Tutti")
	{
		foreach(array_keys($session['users']) as $frompart)
		{
			pay($frompart,$to,$value);
		}
	}
	else if($to=="Tutti")
	{
		foreach(array_keys($session['users']) as $topart)
		{
			pay($from,$topart,$value);
		}
	}
	else
	{
		if($from!="Banca")
		{
			$session['users'][$from]['balance']=$session['users'][$from]['balance']-$value;
			$session['changeid'][makeuserid()[$from]]=$session['changeid'][makeuserid()[$from]]-$value;
			$session['changeid']['timestamp']=time();
		}
		if($to!="Banca")
		{
			$session['users'][$to]['balance']=$session['users'][$to]['balance']+$value;
			$session['changeid'][makeuserid()[$to]]=$session['changeid'][makeuserid()[$to]]+$value;
			$session['changeid']['timestamp']=time();
		}
	}
	update();
}
function newuser()
{
	global $session;
	return array(
		'id'=>++$session['lastid'],
		'cardId'=>"",
		'balance'=>INTBALANCE,
	);
}
function user_exists($user)
{
	global $session;
	return is_array($session['users'][$user]);
}
$changetimeout=10;
$kill=file_get_contents("kill");
$last=file_get_contents("lastcall");
if($last!=0&&$last<time()-INACTIVITY_TIMEOUT||$kill)
{
	mkdir("Saved/");
	rename("session","Saved/".date("r",($kill ? time() : $last)).($kill ? " (killed)" : NULL));
	unlink("lastcall");
	file_put_contents("read",$last.($kill ? " (killed)" : NULL));
}
$session=json_decode(file_get_contents("session"),true);
foreach($_GET as $key=>$val)
{
	if(in_array($val,$session['graveyard']))
	{
		retarray(array("lost"=>true));
		exit;
	}
	$$key=$val;
}
file_put_contents("lastcall",time());
unset($session['change']);
if(time()-$session['changeid']['timestamp']>=10)
{
	unset($session['changeid']);
}
else
{
	foreach($session['changeid'] as $id=>$change)
	{
		$userid=array_search($id,makeuserid());
		if($userid!==false)
		{
			$session['change'][$userid]=$change;
		}
	}
}
$reserved=array("Banca","Tutti");
?>