package com.cisco.hicn.forwarder.preferences;

import java.util.ArrayList;

public class CRUD {

    private ArrayList<String> interfacesArrayList =new ArrayList<>();

    public void save(String interfaceName)
    {
        interfacesArrayList.add(interfaceName);
    }

    public ArrayList<String> getInterfacesArrayList()
    {

        return interfacesArrayList;
    }

    public Boolean update(int position, String newInterfaceName)
    {
       try {
           interfacesArrayList.remove(position);
           interfacesArrayList.add(position,newInterfaceName);

           return true;
       }catch (Exception e)
       {
           e.printStackTrace();
          return false;
        }
    }

    public Boolean delete(int position)
    {
        try {
            interfacesArrayList.remove(position);

            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;

        }
    }
}
