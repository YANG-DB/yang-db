package com.kayhut.fuse.unipop.controller.utils.map;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by roman.margolis on 19/10/2017.
 */
public class MapHelper {
    //region Public Static Methods
    static public <T> Optional<T> value(Map<String, Object> map, String key) {
        List<T> values = values(map, key);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    static public <T> List<T> values(Map<String, Object> map, String key) {
        T val = (T)map.get(key);
        if (val != null) {
            return List.class.isAssignableFrom(val.getClass()) ? (List)val : Collections.singletonList(val);
        }

        if (key.indexOf(".") > 0) {
            String[] path = key.split("\\.");
            return values(map, path, 0);
        }

        return Collections.emptyList();
    }

    static public boolean containsKey(Map<String, Object> map, String key) {
        boolean mapContainsKey = map.containsKey(key);
        if (mapContainsKey) {
            return true;
        }

        if (key.indexOf(".") > 0) {
            String[] path = key.split("\\.");
            return containsKey(map, path, 0);
        }

        return false;
    }
    //endregion

    //region Private Static Methods
    static private <T> List<T> values(Map<String, Object> map, String[] path, int pathIndex) {
        Object mapValue = null;
        for(int index = pathIndex; index < path.length; index++) {
            mapValue = map.get(path[index]);
            if (mapValue == null) {
                return Collections.emptyList();
            }

            if (Map.class.isAssignableFrom(mapValue.getClass())) {
                map = (Map<String, Object>) mapValue;
            } else {
                if (List.class.isAssignableFrom(mapValue.getClass())) {
                    List<T> list = (List<T>)mapValue;
                    if (list.size() > 0 && Map.class.isAssignableFrom(list.get(0).getClass())) {
                        final int pathIndexRec = index + 1;
                        return Stream.ofAll(list)
                                .map(value -> (Map<String, Object>)value)
                                .flatMap(listMap -> MapHelper.<T>values(listMap, path, pathIndexRec))
                                .toJavaList();
                    } else {
                        if (index == path.length - 1) {
                            return list;
                        }
                    }
                }

                if (index < path.length - 1) {
                    return Collections.emptyList();
                }
            }
        }

        return Collections.singletonList((T)mapValue);
    }

    static private boolean containsKey(Map<String, Object> map, String[] path, int pathIndex) {
        Object mapValue = null;
        for(int index = pathIndex; index < path.length; index++) {
            mapValue = map.get(path[index]);
            if (mapValue == null) {
                return false;
            }

            if (Map.class.isAssignableFrom(mapValue.getClass())) {
                map = (Map<String, Object>) mapValue;
            } else {
                if (List.class.isAssignableFrom(mapValue.getClass())) {
                    for(Object obj : (List) mapValue) {
                        if (Map.class.isAssignableFrom(obj.getClass())) {
                            if (containsKey((Map<String, Object>)obj, path, pathIndex + 1)) {
                                return true;
                            }
                        }
                    }
                }

                if (index < path.length - 1) {
                    return false;
                }
            }
        }

        return true;
    }
    //endregion
}
