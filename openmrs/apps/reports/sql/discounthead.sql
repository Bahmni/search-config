select acc.name,sum(discount_amount),count(so.id) from sale_order so inner JOIN
  account_account acc on acc.id = so.discount_acc_id and acc.parent_id=(select id from account_account where name='Discounts')
    and acc.name in('SEARCH employees','SEARCH Village Health Workers','Funds','SEARCH Tribal area','Other reasons')
                         and so.date_confirm BETWEEN '#startDate#' and '#endDate#'
group by acc.name ORDER BY acc.name;