SELECT rpa.city_village as "Village", sum(case when (so.care_setting ILIKE 'ipd') then 1 ELSE 0 end) as "Free Patients IPD" ,
  sum(case when(so.care_setting ILIKE 'ipd')then so.discount_amount else 0 end) as "Discount amount IPD",
  sum(case when(so.care_setting ILIKE 'opd') then 1 else 0 end) as "Free Patients OPD",
  sum(case when(so.care_setting ILIKE 'opd')then so.discount_amount else 0 end) as "Discount amount OPD" from sale_order so INNER JOIN account_account ac on ac.id = so.discount_acc_id
INNER JOIN res_partner_address rpa on rpa.partner_id = so.partner_id
where (so.discount_percentage=100 or so.chargeable_amount=0) and so.state NOT IN ('draft','cancel') and ac.name='Free - SEARCH Tribal area'
      and so.date_confirm BETWEEN '#startDate#' and '#endDate#' GROUP BY rpa.city_village ORDER BY rpa.city_village;