select discount_percentage,count(id) from sale_order
where state not in('draft','cancel') and cast(date_confirm as DATE) BETWEEN '#startDate#' and '#endDate#'
  and care_setting='ipd'
group by discount_percentage ORDER BY discount_percentage ASC;
