package uk.gov.cshr.service.csrs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CivilServantDto {
    private Long userId;
    private String fullName;
    private String lineManagerEmailAddress;
    private Grade grade;
    private OrganisationalUnit organisationalUnit;
    private Profession profession;
    private List<Profession> otherAreasOfWork;
    private Set<OrganisationalUnit> otherOrganisationalUnits;
    private List<Interest> interests;

    public String getDisplayLineManagerEmail() {
        return lineManagerEmailAddress == null ? "None" : lineManagerEmailAddress;
    }

    public String getDisplayOrganisation() {
        return organisationalUnit == null ? "None" : organisationalUnit.toDisplayString();
    }

    public String getDisplayGrade() {
        return grade == null ? "None" : grade.getName();
    }

    public String getDisplayProfession() {
        return profession == null ? "None" : profession.getName();
    }

    public String getDisplayOtherAreasOfWork() {
        return otherAreasOfWork == null || otherAreasOfWork.isEmpty()
                ? "None"
                : otherAreasOfWork.stream().map(Profession::getName).collect(joining(", "));
    }

    public String getDisplayInterests() {
        return interests == null || interests.isEmpty()
                ? "None"
                : interests.stream().map(Interest::getName).collect(joining(", "));
    }

    public boolean isProfileComplete() {
        return organisationalUnit != null
                && profession != null
                && fullName != null
                && (otherAreasOfWork != null && !otherAreasOfWork.isEmpty());
    }
}
