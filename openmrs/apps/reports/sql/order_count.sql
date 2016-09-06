select case when parent='Procedure, procedure' then 'Procedure' else parent end as 'Type of Procedure',count(order_id) as 'Total Orders', sum(case when obs_id is NULL then 0 else 1 end) as Fulfilled_Orders
 from (SELECT o.order_type_id,o.order_id,cn.name parent,o.concept_id,ob.obs_id FROM orders o
inner join concept_set cs on o.concept_id=cs.concept_id
LEFT OUTER JOIN obs ob ON ob.obs_id = (select max(obs_id) from obs where o.order_id = order_id)
Inner join concept_name cn on cs.concept_set=cn.concept_id and concept_name_type='FULLY_SPECIFIED'
where o.order_type_id=5 and date(o.date_created) between '#startDate#' and '#endDate#'
)finalorder group by parent;