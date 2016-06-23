SELECT  name as Discount_Head,care_setting as Visit_Type,sum(
    (case when (discount_percentage=100 or discount_amount=(amount_tax+amount_untaxed)) then 1 else 0 end)
)as Free_Patient_Count, sum(discount_amount) as Total_Discount  from (SELECT acc.name,so.discount_amount,so.discount_percentage,
  so.amount_total,so.amount_untaxed,so.amount_tax,so.date_confirm,so.care_setting
from sale_order so
INNER JOIN account_account acc on acc.id = so.discount_acc_id
where state not in ('draft','cancel')
      and so.date_confirm BETWEEN '2016-01-01' and '2016-07-01') as ss
GROUP BY name,care_setting;