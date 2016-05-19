select rpa.city_village as "Village",so.care_setting as "Free Patients Type",count(so.id) as "Free Patients",sum(so.discount_amount) as "Discount Amount" from res_partner_address rpa
  inner join sale_order so on so.partner_id=rpa.partner_id and so.discount_percentage=100
                              and state not in('draft','cancel') and so.date_confirm BETWEEN '#startDate#' and '#endDate#'
  INNER JOIN account_account acc on acc.id = so.discount_acc_id and acc.name='SEARCH Tribal area'
GROUP BY rpa.city_village,so.care_setting;