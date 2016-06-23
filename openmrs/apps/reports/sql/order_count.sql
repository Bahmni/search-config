select name,count(order_id) as Total_Orders,sum(case when obs_id is NULL then 0 else 1 end) as Fulfilled_Orders
from (SELECT o.order_id,ot.name,order_action,ob.obs_id FROM orders o
INNER JOIN order_type ot on ot.order_type_id=o.order_type_id
  LEFT OUTER JOIN obs ob ON ob.obs_id = (select max(obs_id) from obs where o.order_id = order_id)
WHERE date(o.date_created)
between '#startDate#' and '#endDate#'  and
    o.order_type_id not in (select order_type_id from order_type where name='Lab Order' or name='Drug Order')
              ) xxx
GROUP BY name
;