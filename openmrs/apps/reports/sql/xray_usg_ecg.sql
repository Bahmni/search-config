SELECT name,count(product_id) TOTAL_NUMBERS,sum(product_uom_qty*price_unit) TOTAL_AMOUNT,
  sum(case when (discount_amount = (amount_tax+amount_untaxed)) then 1 else 0 end) as Full_Discount,
  ROUND(sum(product_uom_qty*price_unit*discount_amount/(case when (amount_tax+amount_untaxed=0) then 1 else amount_tax+amount_untaxed end)),2) as totalDiscout
from (select pc.name, product_id,sol.product_uom_qty,sol.price_unit,
  so.discount_amount,so.amount_total,so.amount_untaxed,so.amount_tax
FROM sale_order_line sol
  INNER JOIN product_product pp on pp.id=sol.product_id
INNER JOIN product_template pt on pt.id=pp.product_tmpl_id
  INNER JOIN product_category pc on pt.categ_id=pc.id
                                  and pc.name in ('USG','X-Ray','ECG')
  INNER JOIN sale_order so on so.id=sol.order_id
where sol.state='confirmed' AND DATE(SO.date_confirm) BETWEEN '#startDate#' and '#endDate#' ) as xx
GROUP BY xx.name