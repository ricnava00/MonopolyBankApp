<?php
/*
In:
from
to
value
verified

Out: none
*/
include "../api.php";
if(!empty($session['unverified']))
{
	reterror("C'è un pagamento da verificare dalla banca, attendi prima di aggiungerne uno nuovo");
}
else if((!user_exists($from)&&!in_array($from,$reserved))||(!user_exists($to)&&!in_array($to,$reserved)))
{
	reterror("In qualche modo sei riuscito a selezionare un utente che non esiste");
}
else
{
	if(!in_array($from,$reserved)&&$session['users'][$from]['balance']<$value)
	{
		reterror($from." non ha abbastanza soldi");
	}
	else
	{
		if($from=="Tutti")
		{
			foreach($session['users'] as $name=>$check)
			{
				if($check['balance']<$value)
				{
					reterror($name." non ha abbastanza soldi");
					exit;
				}
			}
		}
		else if($to=="Tutti"&&$from!="Banca")
		{
			if($session['users'][$from]['balance']<$value*(count($session['users'])-1))
			{
				reterror($from." non ha abbastanza soldi");
				exit;
			}
		}
		if($verified)
		{
			pay($from,$to,$value);
		}
		else
		{
			$session['unverified']=array('from'=>$from,'to'=>$to,'value'=>$value);
			update();
		}
		retok();
	}
}
?>