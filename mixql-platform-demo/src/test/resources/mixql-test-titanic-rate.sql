let engine "sqlite-local-titanic";
select
    case when f.survived = 1 then 'Survived' else 'Not Survived' end as Survival_status,
    count(*) as Survival_rate,
    printf("%.2f", 100.0 * count(*) / max(f.total_passeng)) || " %" as Percent,
    max(f.total_passeng) as Total_passengers
from
    (
        select  count(*) over() as total_passeng,
                t.*
        from Observation t
    ) f
group by f.alive_id;
let engine "stub-local";