select acc.name as "Free Patients Categories",so.care_setting as "Free Patients Type",sum(discount_amount) as "Discount Amount",count(so.id) as "Free Patients" from sale_order so inner JOIN
  account_account acc on acc.id = so.discount_acc_id and acc.parent_id=(select id from account_account where name='Discounts')
    and acc.name in('SEARCH employees','SEARCH Village Health Workers','Funds','SEARCH Tribal area','Other reasons')
                        and state not in('draft','cancel') and so.date_confirm BETWEEN '#startDate#' and '#endDate#'
group by acc.name,so.care_setting ORDER BY acc.name;