package com.yangdb.cyber.ontology;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;
import com.yangdb.fuse.dispatcher.convertion.CsvToJsonConverter;
import com.yangdb.fuse.dispatcher.query.sql.DDLToOntologyTransformer;
import com.yangdb.fuse.executor.ontology.schema.DDLToIndexProviderTranslator;
import com.yangdb.fuse.model.ontology.EnumeratedType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.MappingIndexType;
import com.yangdb.fuse.model.schema.Relation;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.yangdb.fuse.dispatcher.query.sql.DDLToOntologyTransformer.*;
import static com.yangdb.fuse.executor.ontology.schema.DDLToIndexProviderTranslator.CREATE_RELATION_BY_FK;
import static java.util.stream.Collectors.groupingBy;

public class CyberDDLToOntologyGenerator {
    public static List<String> tables ;
    public static DDLToOntologyTransformer transformer;
    public static DDLToIndexProviderTranslator indexProviderTranslator;

    @BeforeClass
    public static void setUp() throws Exception {
        tables = new ArrayList<>();
        String sqlPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("sample")).getPath();
        Files.newDirectoryStream(Paths.get(sqlPath),
                path -> path.toString().endsWith(".ddl")).
                forEach(file-> {
                    try {
                        tables.add(new String(Files.readAllBytes(file.toFile().toPath())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        Map config = new HashMap();
        config.put("assembly","Cyber");
        config.put(String.format("%s.%s","Cyber",ENTITIES),Arrays.asList("Behaviors","EnrichmentEvents","Entities","EntitiesProcessesAndFile","Events_Analysis","Traces"));
        config.put(String.format("%s.%s","Cyber",RELATIONSHIPS),Arrays.asList("AlertsToBehaviors", "behavior_to_behavior", "BehaviorEntities", "BehaviorEvents", "TraceEntities", "TraceEvents", "TracesToBehaviors"));
        config.put(String.format("%s.%s","Cyber",DICTIONARY),Arrays.asList("lov_BehaviorsTypes", "lov_CyberObjectTypes", "lov_EventsTypes"));
        transformer = new DDLToOntologyTransformer(ConfigFactory.parseMap(config));
    }

    @Test
    /**
     * test Ontology Creation according to given list of DDL queries
     */
    public void testOntologyCreation() {
        Ontology ontology = transformer.transform("Cyber", tables);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(5,ontology.getEntityTypes().size());
        Assert.assertEquals(6,ontology.getRelationshipTypes().size());
        Assert.assertEquals(312,ontology.getProperties().size());
    }

    @Test
    /**
     * test index provider creation according to given list of DDL queries + ontology
     */
    public void testIndexProviderCreation() {
        indexProviderTranslator =  new DDLToIndexProviderTranslator(ConfigFactory.parseMap(ImmutableMap.of(CREATE_RELATION_BY_FK, true)));
        IndexProvider indexProvider = indexProviderTranslator.translate("Cyber", tables);
        Assert.assertNotNull(indexProvider);

        Map<String, List<Relation>> map = indexProvider.getRelations().stream()
                .filter(r -> r.getPartition().equals(MappingIndexType.UNIFIED.name()))
                .collect(groupingBy(r->r.getProps().getValues().get(0)));

        Assert.assertEquals(17,indexProvider.getEntities().size());
        Assert.assertEquals(10, map.size());
        Assert.assertEquals(38,indexProvider.getRelations().size());

    }

    @Test
    /**
     * test index provider creation according to given list of DDL queries + ontology
     */
    public void testIndexProviderCreationNoFKRelations() {
        indexProviderTranslator =  new DDLToIndexProviderTranslator(ConfigFactory.empty());
        IndexProvider indexProvider = indexProviderTranslator.translate("Cyber", tables);
        Assert.assertNotNull(indexProvider);

        Assert.assertEquals(17,indexProvider.getEntities().size());
        Assert.assertEquals(0,indexProvider.getRelations().size());

    }

    @Test
    /**
     * test index provider creation according to ontology
     */
    public void testIndexProviderCreationFromOntology() {
        Ontology ontology = transformer.transform("Cyber", tables);
        Assert.assertNotNull(ontology);

        IndexProvider indexProvider = IndexProvider.Builder.generate(ontology);
        Assert.assertNotNull(indexProvider);

        Assert.assertEquals(17,indexProvider.getEntities().size());
        Assert.assertEquals(10,indexProvider.getRelations().size());

    }


    @Test
    public void csvLovBehaviorsTypesToEnum() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample/data/lov_BehaviorsTypes.csv");
        assert stream != null;
        String csv = IOUtils.toString(stream, StandardCharsets.UTF_8);
        EnumeratedType type = CsvToJsonConverter.csvToEnum("BehaviorsTypes",0,3,csv);
        Assert.assertEquals(type.toString(),"EnumeratedType [values = [Value [val = -2, name = File Executed], Value [val = -1, name = Create Process], Value [val = 1007, name = Event log message], Value [val = 1041, name = Driver loaded], Value [val = 1048, name = Remote Access], Value [val = 1080, name = Service deleted], Value [val = 1117, name = Check for remote debugger], Value [val = 1144, name = Executable Blocked], Value [val = 1145, name = User Execution blocked], Value [val = 1146, name = Key logger – Set Windows Hook], Value [val = 1152, name = Ransomware blocked], Value [val = 1154, name = Code injection - Process hollowing], Value [val = 1155, name = MBR change blocked], Value [val = 1159, name = Use Domain Generation Algorithm (DGA)], Value [val = 1160, name = Screenshot Taken], Value [val = 1163, name = WMI Event Subscription], Value [val = 1164, name = WMI Event Subscription Change], Value [val = 2007, name = Unknown Double ext. - file created], Value [val = 2015, name = Sensitive file accessed], Value [val = 2017, name = Unknown Double ext. - file renamed], Value [val = 2023, name = Execution from removable media], Value [val = 2029, name = Program files - file created], Value [val = 2030, name = Program files - file deleted], Value [val = 2031, name = Program files - file renamed], Value [val = 2038, name = Suspicious path - execution], Value [val = 2049, name = RTL override - file created], Value [val = 2050, name = RTL override - file renamed], Value [val = 2052, name = Writable code section], Value [val = 2054, name = Abnormal ext. - file created], Value [val = 2057, name = UAC Bypass], Value [val = 2061, name = Abnormal ext. - file renamed], Value [val = 2066, name = Security Settings modification], Value [val = 2072, name = YARA rules matched], Value [val = 2089, name = Reflective DLL loading], Value [val = 2093, name = Masquerading - file created], Value [val = 2095, name = Check for Sandbox Products], Value [val = 2096, name = Access to Windows Authentication Service], Value [val = 2098, name = COM Hijacking - Search Order Hijack], Value [val = 2099, name = Suspicious Command Line], Value [val = 2100, name = Self Delete], Value [val = 2101, name = Self Copy], Value [val = 2102, name = Remote Management], Value [val = 2103, name = Abnormal Process Network Activity], Value [val = 2104, name = Privileged Process In User Folder], Value [val = 2105, name = Create And Execute], Value [val = 2110, name =  Hijacking - COM Object in a Suspicious Folder], Value [val = 2208, name = Key logger – Raw input devices], Value [val = 3000, name = AM Prevention - Known IOC], Value [val = 3002, name = AM Prevention - Behavioral], Value [val = 11005, name = Registry - executable added], Value [val = 11012, name = Network - upload rate], Value [val = 11023, name = Uncommon driver installed], Value [val = 11029, name = System folder - exec. changed], Value [val = 11036, name = LNK file  changed], Value [val = 11038, name = Registry - delete after boot], Value [val = 11039, name = Pentest tool used], Value [val = 11041, name = Child with high privileges], Value [val = 11042, name = Service created], Value [val = 11043, name = Driver created], Value [val = 11044, name = Service changed], Value [val = 11045, name = Driver changed], Value [val = 11046, name = Registry - service added], Value [val = 11047, name = Registry - driver added], Value [val = 11048, name = Registry - autorun added ], Value [val = 11050, name = Abnormal ext. - registry autorun], Value [val = 11051, name = Autorun folder - file added], Value [val = 11055, name = Process executed from Office], Value [val = 11056, name = Suspicious PowerShell Command-line], Value [val = 11057, name = UAC bypass - DLL Hijack], Value [val = 11058, name = Windows Process Suspicious Path], Value [val = 11059, name = Remotely Executed Service - Source], Value [val = 11060, name = Remotely Executed Service - Target], Value [val = 11068, name = Reverse shell], Value [val = 12036, name = LNK file - target changed], Value [val = 13001, name = Code injection - Classical], Value [val = 13002, name = Code injection - Shared section], Value [val = 13003, name = Code injection - APC], Value [val = 13004, name = Code injection - SetThreadContext], Value [val = 13011, name = WMI Execution by PowerShell], Value [val = 13012, name = WMI Script Executed], Value [val = 13013, name = Remote WMI command execution], Value [val = 13014, name = WMI Script Suspicious Traffic], Value [val = 13015, name = WMI Script Executed process], Value [val = 13017, name = WinRM Execution], Value [val = 13021, name = Entity with Revoked Certificate], Value [val = 13022, name = Entity with Invalid Certificate], Value [val = 13023, name = WebShell], Value [val = 13024, name = Office Executing suspicious app], Value [val = 13025, name = Suspicious Execution of Windows Utility], Value [val = 13030, name = Host Scanning], Value [val = 13031, name = Downloaded file executed], Value [val = 13032, name = Outlook attachment executed], Value [val = 13033, name = File executed from an external drive], Value [val = 13034, name = File downloaded from a network share], Value [val = 13841, name = Suspicious Execution of Script File], Value [val = 13842, name = Suspicious Execution by trusted application], Value [val = 13843, name = Potential UAC Bypass], Value [val = 13844, name = Check Locale], Value [val = 13845, name = Execute VBscript using sysinternals Utility], Value [val = 13846, name = Execution using Windows Debugger], Value [val = 13847, name = Execute dll which is stored in alternate data stream], Value [val = 13848, name = Network Configuration/Scanning- abnormal], Value [val = 13849, name = Change file permission], Value [val = 13851, name = Start a Service], Value [val = 13852, name = Suspicious Task Schedule], Value [val = 13853, name = Stop a Service], Value [val = 13854, name = List Running Processes], Value [val = 13855, name = Query Computer/User Info], Value [val = 13856, name = Use Code Injection Utility], Value [val = 13858, name = Modify Users Accounts], Value [val = 13859, name = Delayed Execution], Value [val = 13861, name = Remote file/desktop utility], Value [val = 13862, name = Network Configuration/Scanning], Value [val = 13863, name = Remote Process Execution Service], Value [val = 13864, name = WMI Execution], Value [val = 13865, name = Dump Process Memory], Value [val = 13866, name = Search for Data in Files], Value [val = 13867, name = Lists the Kerberos principal and Kerberos tickets], Value [val = 13869, name = Create Upload or Download Job], Value [val = 13870, name = Execute Script File], Value [val = 13871, name = Run PowerShell script], Value [val = 13872, name = Use Certificate management utility], Value [val = 13873, name = Modify registry permission], Value [val = 13874, name = Modify Registry keys], Value [val = 13875, name = Use Microsoft Virtualisation for PowerShell], Value [val = 13876, name = List Domain Administrators], Value [val = 13877, name = Check for file permission], Value [val = 13878, name = Schedule a task], Value [val = 13879, name = Shut Down Computer], Value [val = 13880, name = list local accounts], Value [val = 13881, name = Impersonation to Another User], Value [val = 13882, name = Dump the registry hive], Value [val = 13883, name = Query Active Directory], Value [val = 13884, name = Modify shadow copy], Value [val = 13885, name = Potential UAC bypass Using Command Line], Value [val = 13886, name = Show List of Services], Value [val = 13887, name = Port Forwarding], Value [val = 13888, name = Execution by trusted application], Value [val = 13889, name = Change file attributes], Value [val = 13890, name = Kill a Process], Value [val = 13891, name = Managing File System Properties], Value [val = 13892, name = Microsoft HTML Application Host], Value [val = 13893, name = Manage Services], Value [val = 13894, name = Search for Stored Credentials], Value [val = 13895, name = Manage File Shares], Value [val = 13896, name = Windows Event Logs], Value [val = 60020, name = new B]], eType = BehaviorsTypes]");
    }

    @Test
    public void csvLovCyberObjectTypesToEnum() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample/data/lov_CyberObjectTypes.csv");
        assert stream != null;
        String csv = IOUtils.toString(stream, StandardCharsets.UTF_8);
        EnumeratedType type = CsvToJsonConverter.csvToEnum("CyberObjects",0,1,csv);
        Assert.assertEquals(type.toString(),"EnumeratedType [values = [Value [val = 0, name = File], Value [val = 1, name = Process], Value [val = 2, name = RegistryValue], Value [val = 3, name = User], Value [val = 4, name = Driver], Value [val = 5, name = Socket], Value [val = 6, name = Group], Value [val = 9, name = RegistreyKey], Value [val = 10, name = NIC], Value [val = 11, name = Routing entry], Value [val = 12, name = SystemInfo], Value [val = 13, name = Session], Value [val = 14, name = Login], Value [val = 15, name = Logoff], Value [val = 16, name = Service], Value [val = 17, name = Event Log], Value [val = 18, name = Disk Drive], Value [val = 19, name = Partition], Value [val = 20, name = BIOS], Value [val = 21, name = Host], Value [val = 22, name = OS], Value [val = 23, name = Page File], Value [val = 24, name = Hot Fix], Value [val = 25, name = Physical Disk], Value [val = 26, name = Kernel Table], Value [val = 28, name = Scheduled Task], Value [val = 50027, name = URL], Value [val = 50028, name = Network Share]], eType = CyberObjects]");
    }

    @Test
    public void csvLovEventsTypesToEnum() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample/data/lov_EventsTypes.csv");
        assert stream != null;
        String csv = IOUtils.toString(stream, StandardCharsets.UTF_8);
        EnumeratedType type = CsvToJsonConverter.csvToEnum("EventsTypes",0,1,csv);
        Assert.assertEquals(type.toString(),"EnumeratedType [values = [Value [val = 2, name = User created], Value [val = 3, name = Group created], Value [val = 5, name = Disk drive added], Value [val = 6, name = Driver created], Value [val = 7, name = Event log message], Value [val = 27, name = NIC added], Value [val = 30, name = Network listen], Value [val = 32, name = Network send], Value [val = 33, name = Network receive], Value [val = 38, name = Process created], Value [val = 40, name = Process load image], Value [val = 41, name = Driver loaded], Value [val = 43, name = Partition loaded], Value [val = 44, name = Process execution Information], Value [val = 45, name = Route entry added], Value [val = 47, name = Service created], Value [val = 48, name = Remote access], Value [val = 49, name = Boot], Value [val = 60, name = Kernel table changed], Value [val = 61, name = Kernel table hooked], Value [val = 73, name = User login], Value [val = 74, name = User log out], Value [val = 75, name = Autorun folder - file changed], Value [val = 77, name = NIC changed], Value [val = 78, name = NIC deleted], Value [val = 79, name = NIC information], Value [val = 80, name = Service deleted], Value [val = 81, name = Service changed], Value [val = 82, name = Service information], Value [val = 83, name = Driver deleted], Value [val = 84, name = Driver changed], Value [val = 85, name = Driver information], Value [val = 86, name = Disk Drive deleted], Value [val = 87, name = Disk Drive changed], Value [val = 88, name = Disk drive information], Value [val = 89, name = Partition changed], Value [val = 90, name = Partition deleted], Value [val = 94, name = User changed], Value [val = 95, name = User deleted], Value [val = 96, name = Group changed], Value [val = 97, name = Group deleted], Value [val = 98, name = Route entry changed], Value [val = 99, name = Route entry deleted], Value [val = 100, name = System info changed], Value [val = 102, name = System info information], Value [val = 103, name = MBR changed], Value [val = 104, name = Bios changed], Value [val = 105, name = Host configuration changed], Value [val = 106, name = OS changed], Value [val = 107, name = Pagefile added], Value [val = 108, name = Pagefile changed], Value [val = 109, name = Pagefile deleted], Value [val = 110, name = Hotfix added], Value [val = 111, name = Hotfix changed], Value [val = 112, name =  Hotfix deleted], Value [val = 113, name = Virtual Alloc EX], Value [val = 114, name = Create remote thread], Value [val = 115, name = Write process memory], Value [val = 117, name = Check for remote debugger], Value [val = 118, name = Process enumeration], Value [val = 119, name = Process enumeration (toll32help)], Value [val = 125, name = Disk drive first seen], Value [val = 126, name = Driver first seen], Value [val = 127, name = Service first seen], Value [val = 128, name = Bios first seen], Value [val = 129, name = Configuration first seen], Value [val = 130, name = OS first seen], Value [val = 131, name = Pagefile first seen], Value [val = 132, name = Hotfix first seen], Value [val = 133, name = Group first seen], Value [val = 134, name = User first seen], Value [val = 135, name = System information first seen], Value [val = 136, name = NIC first seen], Value [val = 137, name = Partition first seen], Value [val = 138, name = Route entry first seen], Value [val = 144, name = Executable Blocked], Value [val = 145, name = User Execution blocked], Value [val = 146, name = Key logger - set windows hook extended], Value [val = 149, name = Set thread context], Value [val = 150, name = Queue user APC], Value [val = 151, name = File encrypted], Value [val = 152, name = Ransomware blocked], Value [val = 153, name = File extension changed], Value [val = 154, name = Process hollowing], Value [val = 155, name = MBR change blocked], Value [val = 156, name = Network send/receive], Value [val = 158, name = Map View of section message], Value [val = 159, name = DNS Domain Generation Algorithm], Value [val = 160, name = Screenshot Taken], Value [val = 161, name = Screenshot Taken], Value [val = 163, name = WMI Event Subscription], Value [val = 164, name = WMI Event Subscription Change], Value [val = 166, name = WMI Event Subscription], Value [val = 1000, name = Alternate data stream write], Value [val = 1007, name = Unknown Double ext. - file created], Value [val = 1015, name = Sensitive file accessed], Value [val = 1017, name = Unknown Double ext. - file renamed], Value [val = 1018, name = Executable created], Value [val = 1019, name = Executable changed], Value [val = 1020, name = Registry - executable added], Value [val = 1023, name = Execution from removable media], Value [val = 1024, name = Executable deleted], Value [val = 1025, name = Script file read], Value [val = 1026, name = Executable renamed], Value [val = 1029, name = Program files - file created], Value [val = 1030, name = Program files - file deleted], Value [val = 1031, name = Program files - file renamed], Value [val = 1032, name = User folders - file created], Value [val = 1034, name = User folders - file renamed], Value [val = 1035, name = User folders - file deleted], Value [val = 1036, name = Process accessed new extension], Value [val = 1038, name = Suspicious path - execution], Value [val = 1039, name = Non-standard entry point], Value [val = 1040, name = No code-section], Value [val = 1041, name = Registry - autorun key created], Value [val = 1042, name = COM Hijacking], Value [val = 1043, name = Non-standard section], Value [val = 1044, name = Missing mandatory section], Value [val = 1045, name = Multiple code sections], Value [val = 1046, name = Last section is code], Value [val = 1047, name = Only code Section], Value [val = 1048, name = Suspicious import], Value [val = 1049, name = RTL override - file created], Value [val = 1050, name = RTL override - file renamed], Value [val = 1051, name = High entropy section], Value [val = 1052, name = Writeable code-section], Value [val = 1053, name = Runtime proxy Dll], Value [val = 1054, name = Abnormal ext. - file created], Value [val = 1057, name = UAC Bypass], Value [val = 1058, name = Entry point is not section], Value [val = 1061, name = Abnormal ext. - file renamed], Value [val = 1062, name = Registry - autorun key deleted], Value [val = 1063, name = Registry - autorun value deleted], Value [val = 1065, name = Registry Key created], Value [val = 1066, name = Security Settings modification], Value [val = 1067, name = Security Settings modification], Value [val = 1068, name = Registry value deleted], Value [val = 1069, name = Lnk file created], Value [val = 1070, name = Lnk file changed], Value [val = 1071, name = Lnk file renamed], Value [val = 1072, name = YARA rules matched], Value [val = 1073, name = YARA rules skipped scan], Value [val = 1074, name = DNS request], Value [val = 1075, name = DNS unusual query], Value [val = 1076, name = Http request], Value [val = 1077, name = Http malformed request], Value [val = 1078, name = Scheduled task created], Value [val = 1079, name = Scheduled task changed], Value [val = 1080, name = Registry key renamed], Value [val = 1083, name = User folders - file changed], Value [val = 1084, name = Program files - file changed], Value [val = 1086, name = Certificate file created], Value [val = 1087, name = Autorun folder - file created], Value [val = 1088, name = Autorun folder - file renamed], Value [val = 1089, name = Reflective DLL loading], Value [val = 1090, name = Abnormal ext. - file changed], Value [val = 1091, name = Certificate file renamed], Value [val = 1092, name = Certificate file changed], Value [val = 1093, name = Known Double ext. - file created], Value [val = 1094, name = Known Double ext. - file renamed], Value [val = 1095, name = Check for Sandbox Products], Value [val = 1096, name = Access to Windows Authentication Service], Value [val = 1097, name = Remote Thread Create Message], Value [val = 1098, name = COM Hijacking - Search Order Hijack], Value [val = 1099, name = Suspicious Command Line], Value [val = 1100, name = Self Delete], Value [val = 1101, name = Self Copy], Value [val = 1102, name = Remote Management], Value [val = 1103, name = Abnormal Process Network Activity], Value [val = 1104, name = Privileged Process In User Folder], Value [val = 1105, name = Create And Execute], Value [val = 1106, name = File moved], Value [val = 1107, name = File modified], Value [val = 1108, name = Key logger - register raw input device], Value [val = 1109, name = File Saved By Outlook Msg], Value [val = 1110, name = COM Hijacking - COM Object in a Suspicious Folder], Value [val = 1111, name = Nt MapView Of SectionNtDll ], Value [val = 1112, name = Nt Dll File Read], Value [val = 1113, name = Thread handler with TSC], Value [val = 2000, name = AM Detection - Known IOC], Value [val = 2002, name = AM Prevention - Behavioral], Value [val = 50001, name = Insight - Executable to Registry], Value [val = 50003, name = Insight- Old name to New name], Value [val = 50005, name = Insight- File Origin]], eType = EventsTypes]");
    }

    @Test
    public void csvLovTracesTypesToEnum() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample/data/lov_TracesTypes.csv");
        assert stream != null;
        String csv = IOUtils.toString(stream, StandardCharsets.UTF_8);
        EnumeratedType type = CsvToJsonConverter.csvToEnum("TracesTypes",0,1,csv);
        Assert.assertEquals(type.toString(),"EnumeratedType [values = [Value [val = 1, name = Sequence based], Value [val = 2, name = Time based], Value [val = 3, name = Graph based], Value [val = 4, name = Immediate trace based]], eType = TracesTypes]");
    }
}
