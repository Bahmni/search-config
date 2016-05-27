select pp.name_template as "Equipment use",count(sol.id) as "Frequency of use" from
  sale_order so INNER JOIN sale_order_line sol on so.id = sol.order_id and so.date_order BETWEEN '#startDate#' and '#endDate#'
inner join product_product pp on pp.id = sol.product_id and sol.state not in('draft','cancel')
inner join product_template pt on pt.id = pp.product_tmpl_id
INNER JOIN product_category pc on pc.id = pt.categ_id and pc.name='Equipment'
GROUP BY pp.name_template;
